package com.example.ardrawing.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

// CompositionLocal for accessing AuthManager throughout the app
val LocalAuthManager = staticCompositionLocalOf<AuthManager?> { null }

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )
    
    private val _anonymousUserId = MutableStateFlow<String?>(null)
    val anonymousUserId: StateFlow<String?> = _anonymousUserId.asStateFlow()
    
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()
    
    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_ANONYMOUS_USER_ID = "anonymous_user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val USERS_COLLECTION = "users"
    }
    
    init {
        // Load stored user ID if exists
        val storedUserId = prefs.getString(KEY_ANONYMOUS_USER_ID, null)
        _anonymousUserId.value = storedUserId
        Log.d(TAG, "AuthManager initialized with stored ID: $storedUserId")
    }
    
    /**
     * Get unique device ID that persists across app reinstalls
     * Uses Android's ANDROID_ID which is unique per device
     */
    private fun getDeviceId(): String {
        // Check if we already have a stored device ID
        val storedDeviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (storedDeviceId != null) {
            Log.d(TAG, "Using stored device ID: $storedDeviceId")
            return storedDeviceId
        }
        
        // Get Android's unique device ID
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        
        // Create a prefixed device ID
        val deviceId = "device_$androidId"
        
        // Store it for future use
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        Log.d(TAG, "Generated new device ID: $deviceId")
        return deviceId
    }
    
    /**
     * Sign in anonymously to Firebase
     * Creates a new anonymous user or retrieves existing one based on device ID
     */
    suspend fun signInAnonymously(): Result<String> {
        return try {
            _isAuthenticating.value = true
            Log.d(TAG, "========================================")
            Log.d(TAG, "🔐 Starting anonymous sign-in...")
            Log.d(TAG, "========================================")
            
            val deviceId = getDeviceId()
            Log.d(TAG, "📱 Device ID: $deviceId")
            
            // Check if user with this device ID already exists in Firestore
            Log.d(TAG, "🔍 Checking for existing user in Firestore...")
            val existingUser = checkExistingUser(deviceId)
            
            if (existingUser != null) {
                Log.d(TAG, "✅ Found existing user: $existingUser")
                _anonymousUserId.value = existingUser
                prefs.edit().putString(KEY_ANONYMOUS_USER_ID, existingUser).apply()
                _isAuthenticating.value = false
                Log.d(TAG, "========================================")
                return Result.success(existingUser)
            }
            
            Log.d(TAG, "🆕 No existing user found, creating new Firebase user...")
            
            // Sign in anonymously to Firebase
            val result = auth.signInAnonymously().await()
            val firebaseUserId = result.user?.uid
            
            if (firebaseUserId != null) {
                Log.d(TAG, "✅ Firebase anonymous sign-in successful!")
                Log.d(TAG, "🆔 New User ID: $firebaseUserId")
                
                // Store user data in Firestore with device ID
                Log.d(TAG, "💾 Saving user data to Firestore...")
                saveUserToFirestore(firebaseUserId, deviceId)
                
                // Store locally
                _anonymousUserId.value = firebaseUserId
                prefs.edit().putString(KEY_ANONYMOUS_USER_ID, firebaseUserId).apply()
                Log.d(TAG, "✅ User ID stored locally")
                
                _isAuthenticating.value = false
                Log.d(TAG, "========================================")
                Result.success(firebaseUserId)
            } else {
                Log.e(TAG, "❌ Firebase user ID is null after sign-in")
                _isAuthenticating.value = false
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to get user ID"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during anonymous sign-in: ${e.message}", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            _isAuthenticating.value = false
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    /**
     * Check if a user with this device ID already exists in Firestore
     */
    private suspend fun checkExistingUser(deviceId: String): String? {
        return try {
            Log.d(TAG, "🔍 Querying Firestore for device: $deviceId")
            val querySnapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .await()
            
            Log.d(TAG, "📊 Query completed. Empty: ${querySnapshot.isEmpty}, Size: ${querySnapshot.size()}")
            
            if (!querySnapshot.isEmpty) {
                val userId = querySnapshot.documents[0].id
                Log.d(TAG, "✅ Found existing user: $userId for device: $deviceId")
                userId
            } else {
                Log.d(TAG, "ℹ️ No existing user found for device: $deviceId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking existing user: ${e.message}", e)
            null
        }
    }
    
    /**
     * Save user data to Firestore
     */
    private suspend fun saveUserToFirestore(userId: String, deviceId: String) {
        try {
            val userData = hashMapOf(
                "userId" to userId,
                "deviceId" to deviceId,
                "createdAt" to System.currentTimeMillis(),
                "lastLoginAt" to System.currentTimeMillis(),
                "isAnonymous" to true
            )
            
            Log.d(TAG, "💾 Writing to Firestore collection: $USERS_COLLECTION")
            Log.d(TAG, "💾 Document ID: $userId")
            
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(userData)
                .await()
            
            Log.d(TAG, "✅ User data saved to Firestore successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving user to Firestore: ${e.message}", e)
            Log.e(TAG, "⚠️ App will continue without Firestore sync")
            // Don't throw - we can still use the app without Firestore
        }
    }
    
    /**
     * Update last login timestamp
     */
    suspend fun updateLastLogin() {
        try {
            val userId = _anonymousUserId.value ?: return
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()
            Log.d(TAG, "Updated last login for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last login", e)
        }
    }
    
    /**
     * Get current anonymous user ID
     */
    fun getCurrentUserId(): String? {
        return _anonymousUserId.value
    }
    
    /**
     * Get device ID
     */
    fun getStoredDeviceId(): String {
        return getDeviceId()
    }
    
    /**
     * Sign out (mainly for testing purposes)
     */
    fun signOut() {
        auth.signOut()
        _anonymousUserId.value = null
        prefs.edit().clear().apply()
        Log.d(TAG, "User signed out")
    }
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return _anonymousUserId.value != null || auth.currentUser != null
    }
}
