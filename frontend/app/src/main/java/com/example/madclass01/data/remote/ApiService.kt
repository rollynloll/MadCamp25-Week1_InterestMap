package com.example.madclass01.data.remote

import com.example.madclass01.data.remote.dto.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Backend FastAPI API 서비스 인터페이스
 */
interface ApiService {
    
    @GET("/")
    suspend fun getHealthCheck(): Response<HealthCheckResponse>
    
    @GET("/health")
    suspend fun getHealth(): Response<HealthCheckResponse>
    
    // Test APIs (개발용)
    @POST("/api/users/test")
    suspend fun createTestUser(@Body request: CreateUserRequest): Response<UserResponse>
    
    @GET("/api/users/test/{userId}")
    suspend fun getTestUser(@Path("userId") userId: String): Response<UserResponse>
    
    // User APIs
    @POST("/api/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserResponse>
    
    @GET("/api/users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserResponse>
    
    @PUT("/api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>
    
    // Photo APIs
    @Multipart
    @POST("/api/photos")
    suspend fun uploadPhoto(
        @Part("user_id") userId: RequestBody,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<PhotoResponse>
    
    @GET("/api/photos/user/{userId}")
    suspend fun getUserPhotos(@Path("userId") userId: String): Response<List<PhotoResponse>>
    
    // Group APIs
    @POST("/api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupResponse>
    
    @GET("/api/groups/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: String): Response<GroupResponse>
    
    @GET("/api/groups/user/{userId}")
    suspend fun getUserGroups(@Path("userId") userId: String): Response<List<GroupResponse>>
    
    @POST("/api/groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: String,
        @Body request: AddMemberRequest
    ): Response<GroupResponse>


        @GET("/api/groups/{groupId}/detail")
        suspend fun getGroupDetail(@Path("groupId") groupId: String): Response<GroupDetailResponse>

        @GET("/api/groups/{groupId}/embeddings")
        suspend fun getGroupUserEmbeddings(@Path("groupId") groupId: String): Response<GroupEmbeddingResponse>

        @GET("/api/users/{userId}/embedding")
        suspend fun getUserEmbedding(@Path("userId") userId: String): Response<UserEmbeddingResponse>

    // ==================== Group Chat APIs ====================

    @GET("/api/chats/group/{groupId}/messages")
    suspend fun getGroupMessages(
        @Path("groupId") groupId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessageResponse>>

    @POST("/api/chats/group/{groupId}/messages")
    suspend fun sendGroupTextMessage(
        @Path("groupId") groupId: String,
        @Body request: SendChatMessageRequest
    ): Response<ChatMessageResponse>

    @Multipart
    @POST("/api/chats/group/{groupId}/photos")
    suspend fun sendGroupImageMessage(
        @Path("groupId") groupId: String,
        @Part("user_id") userId: String,
        @Part("file") file: okhttp3.MultipartBody.Part
    ): Response<ChatMessageResponse>

    @POST("/api/chats/group/{groupId}/join")
    suspend fun joinGroupChat(
        @Path("groupId") groupId: String,
        @Body request: JoinLeaveRequest
    ): Response<Unit>

    @POST("/api/chats/group/{groupId}/leave")
    suspend fun leaveGroupChat(
        @Path("groupId") groupId: String,
        @Body request: JoinLeaveRequest
    ): Response<Unit>

    // ==================== Invite APIs ====================

    @POST("/api/invites/generate")
    suspend fun generateInviteLink(@Body request: GenerateInviteLinkRequest): Response<InviteLinkResponse>

    @GET("/api/invites/group/{groupId}")
    suspend fun getInviteLink(@Path("groupId") groupId: String): Response<InviteLinkResponse>

    @POST("/api/invites/join")
    suspend fun joinByInviteLink(@Body request: JoinByInviteLinkRequest): Response<JoinByInviteLinkResponse>

    // ==================== Image Analysis APIs ====================
    
    @POST("/api/analyze/images")
    suspend fun analyzeImages(@Body request: ImageAnalysisRequest): Response<ImageAnalysisResponse>
    
    @POST("/api/generate-embedding")
    suspend fun generateEmbedding(@Body request: GenerateEmbeddingRequest): Response<EmbeddingResponse>
}
