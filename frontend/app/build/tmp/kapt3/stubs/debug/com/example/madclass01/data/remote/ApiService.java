package com.example.madclass01.data.remote;

/**
 * Backend FastAPI API 서비스 인터페이스
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J(\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0007\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u0007\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001e\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010!\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ\u001e\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010!\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ$\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040$0\u00032\b\b\u0001\u0010!\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ$\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0$0\u00032\b\b\u0001\u0010!\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ(\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010!\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020(H\u00a7@\u00a2\u0006\u0002\u0010)J(\u0010*\u001a\b\u0012\u0004\u0012\u00020&0\u00032\b\b\u0001\u0010!\u001a\u00020+2\b\b\u0001\u0010,\u001a\u00020-H\u00a7@\u00a2\u0006\u0002\u0010.\u00a8\u0006/"}, d2 = {"Lcom/example/madclass01/data/remote/ApiService;", "", "addGroupMember", "Lretrofit2/Response;", "Lcom/example/madclass01/data/remote/dto/GroupResponse;", "groupId", "", "request", "Lcom/example/madclass01/data/remote/dto/AddMemberRequest;", "(Ljava/lang/String;Lcom/example/madclass01/data/remote/dto/AddMemberRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "analyzeImages", "Lcom/example/madclass01/data/remote/dto/ImageAnalysisResponse;", "Lcom/example/madclass01/data/remote/dto/ImageAnalysisRequest;", "(Lcom/example/madclass01/data/remote/dto/ImageAnalysisRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createGroup", "Lcom/example/madclass01/data/remote/dto/CreateGroupRequest;", "(Lcom/example/madclass01/data/remote/dto/CreateGroupRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createTestUser", "Lcom/example/madclass01/data/remote/dto/UserResponse;", "Lcom/example/madclass01/data/remote/dto/CreateUserRequest;", "(Lcom/example/madclass01/data/remote/dto/CreateUserRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createUser", "generateEmbedding", "Lcom/example/madclass01/data/remote/dto/EmbeddingResponse;", "Lcom/example/madclass01/data/remote/dto/GenerateEmbeddingRequest;", "(Lcom/example/madclass01/data/remote/dto/GenerateEmbeddingRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getGroup", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getHealth", "Lcom/example/madclass01/data/remote/dto/HealthCheckResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getHealthCheck", "getTestUser", "userId", "getUser", "getUserGroups", "", "getUserPhotos", "Lcom/example/madclass01/data/remote/dto/PhotoResponse;", "updateUser", "Lcom/example/madclass01/data/remote/dto/UpdateUserRequest;", "(Ljava/lang/String;Lcom/example/madclass01/data/remote/dto/UpdateUserRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadPhoto", "Lokhttp3/RequestBody;", "file", "Lokhttp3/MultipartBody$Part;", "(Lokhttp3/RequestBody;Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface ApiService {
    
    @retrofit2.http.GET(value = "/")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getHealthCheck(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.HealthCheckResponse>> $completion);
    
    @retrofit2.http.GET(value = "/health")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getHealth(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.HealthCheckResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/users/test")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createTestUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.CreateUserRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.UserResponse>> $completion);
    
    @retrofit2.http.GET(value = "/api/users/test/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTestUser(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.UserResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/users")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.CreateUserRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.UserResponse>> $completion);
    
    @retrofit2.http.GET(value = "/api/users/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUser(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.UserResponse>> $completion);
    
    @retrofit2.http.PUT(value = "/api/users/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUser(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.UpdateUserRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.UserResponse>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "/api/photos")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadPhoto(@retrofit2.http.Part(value = "user_id")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody userId, @retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.PhotoResponse>> $completion);
    
    @retrofit2.http.GET(value = "/api/photos/user/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserPhotos(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.example.madclass01.data.remote.dto.PhotoResponse>>> $completion);
    
    @retrofit2.http.POST(value = "/api/groups")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.CreateGroupRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.GroupResponse>> $completion);
    
    @retrofit2.http.GET(value = "/api/groups/{groupId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getGroup(@retrofit2.http.Path(value = "groupId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String groupId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.GroupResponse>> $completion);
    
    @retrofit2.http.GET(value = "/api/groups/user/{userId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserGroups(@retrofit2.http.Path(value = "userId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.example.madclass01.data.remote.dto.GroupResponse>>> $completion);
    
    @retrofit2.http.POST(value = "/api/groups/{groupId}/members")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addGroupMember(@retrofit2.http.Path(value = "groupId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String groupId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.AddMemberRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.GroupResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/analyze/images")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object analyzeImages(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.ImageAnalysisRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.ImageAnalysisResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/generate-embedding")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object generateEmbedding(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.remote.dto.GenerateEmbeddingRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.madclass01.data.remote.dto.EmbeddingResponse>> $completion);
}