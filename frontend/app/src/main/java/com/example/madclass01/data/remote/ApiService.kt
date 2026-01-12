package com.example.madclass01.data.remote

import com.example.madclass01.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Backend FastAPI API 서비스 인터페이스
 * 
 * Base URL: http://localhost:8000 (개발) 또는 배포 서버
 * API Version: 1.0.0
 * 인증: Bearer Token (Authorization 헤더)
 */
interface ApiService {
    
    // ==================== Health Check ====================
    
    @GET("/")
    suspend fun getHealthCheck(): Response<HealthCheckResponse>
    
    @GET("/health")
    suspend fun getHealth(): Response<HealthCheckResponse>
    
    // ==================== 인증 (Auth) ====================
    
    /**
     * Kakao 인증 및 JWT 토큰 발급
     * POST /auth/kakao
     */
    @POST("/auth/kakao")
    suspend fun kakaoLogin(@Body request: KakaoAuthRequest): Response<AuthResponse>
    
    // ==================== 사용자 정보 (Me) ====================
    
    /**
     * 내 정보 조회
     * GET /me
     */
    @GET("/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String
    ): Response<MeResponse>
    
    /**
     * 내 정보 수정
     * PATCH /me
     */
    @PATCH("/me")
    suspend fun updateMe(
        @Body request: MeUpdateRequest,
        @Header("Authorization") authorization: String
    ): Response<OkResponse>
    
    // ==================== 사진 관리 (Photos) ====================
    
    /**
     * 사진 추가 (URL 기반)
     * POST /me/photos
     */
    @POST("/me/photos")
    suspend fun addPhoto(
        @Body request: PhotoCreateRequest,
        @Header("Authorization") authorization: String
    ): Response<MePhoto>
    
    /**
     * 사진 순서 변경
     * PATCH /me/photos/order
     */
    @PATCH("/me/photos/order")
    suspend fun updatePhotoOrder(
        @Body request: PhotoOrderRequest,
        @Header("Authorization") authorization: String
    ): Response<OkResponse>
    
    /**
     * 대표 사진 설정
     * POST /me/photos/{photo_id}/primary
     */
    @POST("/me/photos/{photo_id}/primary")
    suspend fun setPrimaryPhoto(
        @Path("photo_id") photoId: String,
        @Header("Authorization") authorization: String
    ): Response<OkResponse>
    
    /**
     * 사진 삭제
     * DELETE /me/photos/{photo_id}
     */
    @DELETE("/me/photos/{photo_id}")
    suspend fun deletePhoto(
        @Path("photo_id") photoId: String,
        @Header("Authorization") authorization: String
    ): Response<OkResponse>
    
    // ==================== 그룹 (Groups) ====================
    
    /**
     * 그룹 목록 조회
     * GET /groups
     */
    @GET("/groups")
    suspend fun getGroups(
        @Header("Authorization") authorization: String
    ): Response<List<GroupListItem>>
    
    /**
     * 그룹 참여 (인증 없이 user_id 전달)
     * POST /api/groups/{groupId}/members
     */
    @POST("/api/groups/{groupId}/members")
    suspend fun joinGroup(
        @Path("groupId") groupId: String,
        @Body request: AddMemberRequest
    ): Response<GroupResponse>
    
    /**
     * 그룹 멤버 조회
     * GET /groups/{group_id}/members
     */
    @GET("/groups/{group_id}/members")
    suspend fun getGroupMembers(
        @Path("group_id") groupId: String,
        @Header("Authorization") authorization: String
    ): Response<List<GroupMemberItem>>
    
    /**
     * 그룹 Interest Map 조회 (시각화용 좌표 포함)
     * GET /groups/{group_id}/interest-map
     */
    @GET("/groups/{group_id}/interest-map")
    suspend fun getGroupInterestMap(
        @Path("group_id") groupId: String,
        @Header("Authorization") authorization: String
    ): Response<InterestMapResponse>
    
    // ==================== 그룹 메시지 (Messages) ====================
    
    /**
     * 그룹 메시지 조회
     * GET /groups/{group_id}/messages
     */
    @GET("/groups/{group_id}/messages")
    suspend fun getGroupMessages(
        @Path("group_id") groupId: String,
        @Query("limit") limit: Int = 50,
        @Header("Authorization") authorization: String
    ): Response<List<MessageContent>>
    
    /**
     * 그룹 텍스트 메시지 전송
     * POST /groups/{group_id}/messages
     */
    @POST("/groups/{group_id}/messages")
    suspend fun sendGroupMessage(
        @Path("group_id") groupId: String,
        @Body request: MessageCreateRequest,
        @Header("Authorization") authorization: String
    ): Response<MessageContent>
    
    /**
     * 그룹 이미지 메시지 전송
     * POST /groups/{groupId}/photos
     */
    @Multipart
    @POST("/groups/{groupId}/photos")
    suspend fun sendGroupImageMessage(
        @Path("groupId") groupId: String,
        @Part("user_id") userId: String,
        @Part file: MultipartBody.Part
    ): Response<GroupMessageItem>
    
    // ==================== 임베딩 (Embedding) ====================
    
    /**
     * 임베딩 재생성
     * POST /me/embedding/rebuild
     */
    @POST("/me/embedding/rebuild")
    suspend fun rebuildEmbedding(
        @Header("Authorization") authorization: String
    ): Response<MeEmbedding>

    // ==================== App APIs ====================

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

    /**
     * 다중 사진 업로드 (배치)
     * POST /api/photos/batch
     */
    @Multipart
    @POST("/api/photos/batch")
    suspend fun uploadPhotos(
        @Part("user_id") userId: RequestBody,
        @Part files: List<okhttp3.MultipartBody.Part>,
        @Part("selected_tags_json") selectedTagsJson: RequestBody
    ): Response<BatchPhotoUploadResponse>

    @GET("/api/photos/user/{userId}")
    suspend fun getUserPhotos(@Path("userId") userId: String): Response<List<PhotoResponse>>

    // Group APIs
    @POST("/api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupResponse>

    @GET("/api/groups")
    suspend fun getAllGroups(): Response<List<GroupResponse>>

    @GET("/api/groups/user/{userId}")
    suspend fun getUserGroups(@Path("userId") userId: String): Response<List<GroupResponse>>

    @POST("/api/groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: String,
        @Body request: AddMemberRequest
    ): Response<GroupResponse>

    @Multipart
    @POST("/api/groups/{groupId}/profile-image")
    suspend fun uploadGroupProfileImage(
        @Path("groupId") groupId: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<GroupResponse>

    // Group Detail / Embedding
    @GET("/api/groups/{groupId}/detail")
    suspend fun getGroupDetail(@Path("groupId") groupId: String): Response<GroupDetailResponse>

    @GET("/api/groups/{groupId}/embeddings")
    suspend fun getGroupUserEmbeddings(
        @Path("groupId") groupId: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Response<GroupEmbeddingResponse>

    @GET("/api/users/{userId}/embedding")
    suspend fun getUserEmbedding(@Path("userId") userId: String): Response<UserEmbeddingResponse>

    @POST("/api/groups/{groupId}/members")
    suspend fun joinGroupChat(
        @Path("groupId") groupId: String,
        @Body request: AddMemberRequest
    ): Response<GroupResponse>

    @POST("/groups/{groupId}/leave")
    suspend fun leaveGroupChat(
        @Path("groupId") groupId: String
    ): Response<OkResponse>

    // Invite APIs
    @POST("/api/invites/generate")
    suspend fun generateInviteLink(@Body request: GenerateInviteLinkRequest): Response<InviteLinkResponse>

    @GET("/api/invites/group/{groupId}")
    suspend fun getInviteLink(@Path("groupId") groupId: String): Response<InviteLinkResponse>

    @POST("/api/invites/join")
    suspend fun joinByInviteLink(@Body request: JoinByInviteLinkRequest): Response<JoinByInviteLinkResponse>

    // Image Analysis
    @POST("/api/analyze/images")
    suspend fun analyzeImages(@Body request: ImageAnalysisRequest): Response<ImageAnalysisResponse>

    @POST("/api/generate-embedding")
    suspend fun generateEmbedding(@Body request: GenerateEmbeddingRequest): Response<EmbeddingResponse>
}
