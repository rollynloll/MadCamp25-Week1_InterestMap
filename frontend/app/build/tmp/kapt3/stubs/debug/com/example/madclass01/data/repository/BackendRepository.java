package com.example.madclass01.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J$\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u000bJ*\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u00062\u0006\u0010\n\u001a\u00020\t2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J0\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u0012\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\t2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\tH\u0086@\u00a2\u0006\u0002\u0010\u0015J0\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00062\u0006\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\t2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\tH\u0086@\u00a2\u0006\u0002\u0010\u0015JT\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00170\u00062\u0006\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\t2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\t2\u0016\b\u0002\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u001eH\u0086@\u00a2\u0006\u0002\u0010\u001fJ^\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u00062\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u001a\u001a\u00020\t2\b\u0010\"\u001a\u0004\u0018\u00010#2\b\u0010$\u001a\u0004\u0018\u00010\t2\b\u0010%\u001a\u0004\u0018\u00010\t2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\t0\u000f2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\t0\u000fH\u0086@\u00a2\u0006\u0002\u0010(J\u001c\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J\u001c\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00170\u00062\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J\u001c\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00170\u00062\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J\"\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000f0\u00062\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J\"\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020/0\u000f0\u00062\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010*J\u0014\u00100\u001a\b\u0012\u0004\u0012\u0002010\u0006H\u0086@\u00a2\u0006\u0002\u00102JL\u00103\u001a\b\u0012\u0004\u0012\u00020\u00170\u00062\u0006\u0010\n\u001a\u00020\t2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\t2\u0016\b\u0002\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u001eH\u0086@\u00a2\u0006\u0002\u00104J$\u00105\u001a\b\u0012\u0004\u0012\u00020/0\u00062\u0006\u0010\n\u001a\u00020\t2\u0006\u00106\u001a\u000207H\u0086@\u00a2\u0006\u0002\u00108R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00069"}, d2 = {"Lcom/example/madclass01/data/repository/BackendRepository;", "", "apiService", "Lcom/example/madclass01/data/remote/ApiService;", "(Lcom/example/madclass01/data/remote/ApiService;)V", "addGroupMember", "Lcom/example/madclass01/data/repository/ApiResult;", "Lcom/example/madclass01/data/remote/dto/GroupResponse;", "groupId", "", "userId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "analyzeImages", "Lcom/example/madclass01/data/remote/dto/ImageAnalysisResponse;", "imageUrls", "", "(Ljava/lang/String;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createGroup", "name", "creatorId", "description", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createTestUser", "Lcom/example/madclass01/data/remote/dto/UserResponse;", "provider", "providerUserId", "nickname", "createUser", "profileImageUrl", "profileData", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateEmbedding", "Lcom/example/madclass01/data/remote/dto/EmbeddingResponse;", "age", "", "region", "bio", "tags", "imageKeywords", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getGroup", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTestUser", "getUser", "getUserGroups", "getUserPhotos", "Lcom/example/madclass01/data/remote/dto/PhotoResponse;", "healthCheck", "Lcom/example/madclass01/data/remote/dto/HealthCheckResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUser", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadPhoto", "file", "Ljava/io/File;", "(Ljava/lang/String;Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class BackendRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.data.remote.ApiService apiService = null;
    
    @javax.inject.Inject()
    public BackendRepository(@org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.ApiService apiService) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object healthCheck(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.HealthCheckResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createTestUser(@org.jetbrains.annotations.NotNull()
    java.lang.String provider, @org.jetbrains.annotations.NotNull()
    java.lang.String providerUserId, @org.jetbrains.annotations.Nullable()
    java.lang.String nickname, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.UserResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getTestUser(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.UserResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createUser(@org.jetbrains.annotations.NotNull()
    java.lang.String provider, @org.jetbrains.annotations.NotNull()
    java.lang.String providerUserId, @org.jetbrains.annotations.Nullable()
    java.lang.String nickname, @org.jetbrains.annotations.Nullable()
    java.lang.String profileImageUrl, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, ? extends java.lang.Object> profileData, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.UserResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUser(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.UserResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateUser(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.Nullable()
    java.lang.String nickname, @org.jetbrains.annotations.Nullable()
    java.lang.String profileImageUrl, @org.jetbrains.annotations.Nullable()
    java.util.Map<java.lang.String, ? extends java.lang.Object> profileData, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.UserResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object uploadPhoto(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.io.File file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.PhotoResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserPhotos(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<? extends java.util.List<com.example.madclass01.data.remote.dto.PhotoResponse>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createGroup(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String creatorId, @org.jetbrains.annotations.Nullable()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.GroupResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getGroup(@org.jetbrains.annotations.NotNull()
    java.lang.String groupId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.GroupResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserGroups(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<? extends java.util.List<com.example.madclass01.data.remote.dto.GroupResponse>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addGroupMember(@org.jetbrains.annotations.NotNull()
    java.lang.String groupId, @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.GroupResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object analyzeImages(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> imageUrls, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.ImageAnalysisResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateEmbedding(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String nickname, @org.jetbrains.annotations.Nullable()
    java.lang.Integer age, @org.jetbrains.annotations.Nullable()
    java.lang.String region, @org.jetbrains.annotations.Nullable()
    java.lang.String bio, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> imageKeywords, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.madclass01.data.repository.ApiResult<com.example.madclass01.data.remote.dto.EmbeddingResponse>> $completion) {
        return null;
    }
}