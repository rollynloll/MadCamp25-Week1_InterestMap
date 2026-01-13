package com.example.madclass01.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madclass01.domain.model.Group
import com.example.madclass01.domain.model.RelationshipGraph
import com.example.madclass01.domain.model.UserEmbedding
import com.example.madclass01.data.remote.dto.SubgroupClusterRequest
import com.example.madclass01.data.remote.dto.SubgroupItemResponse
import com.example.madclass01.data.repository.ApiResult
import com.example.madclass01.data.repository.BackendRepository
import com.example.madclass01.domain.usecase.CreateGroupUseCase
import com.example.madclass01.domain.usecase.GetGroupDetailUseCase
import com.example.madclass01.domain.usecase.GetRelationshipGraphUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt
import kotlin.random.Random

data class ClusterGroup(
    val id: Int,
    val members: List<UserEmbedding>
)

data class GroupClusterUiState(
    val group: Group? = null,
    val relationshipGraph: RelationshipGraph? = null,
    val clusters: List<ClusterGroup> = emptyList(),
    val clusterCount: Int = 3,
    val subgroupRooms: Map<Int, SubgroupRoom> = emptyMap(),
    val enterRoom: SubgroupRoom? = null,
    val isCreatingRoom: Boolean = false,
    val isSavingCluster: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val customClusterNames: Map<Int, String> = emptyMap() // Key: Cluster ID (Index), Value: Custom Name
)

data class SubgroupRoom(
    val id: String,
    val name: String,
    val clusterIndex: Int,
    val memberCount: Int
)

@HiltViewModel
class GroupClusterViewModel @Inject constructor(
    private val getGroupDetailUseCase: GetGroupDetailUseCase,
    private val getRelationshipGraphUseCase: GetRelationshipGraphUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupClusterUiState())
    val uiState: StateFlow<GroupClusterUiState> = _uiState.asStateFlow()

    fun load(groupId: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            val groupResult = getGroupDetailUseCase(groupId)
            groupResult.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "그룹 정보 조회 실패"
                )
                return@launch
            }

            val graphResult = getRelationshipGraphUseCase(groupId, currentUserId)
            graphResult.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    group = groupResult.getOrNull(),
                    isLoading = false,
                    errorMessage = error.message ?: "관계 그래프 조회 실패"
                )
                return@launch
            }

            val relationshipGraph = graphResult.getOrNull()
            val clusterCount = _uiState.value.clusterCount
            val embeddings = relationshipGraph?.embeddings?.values?.toList().orEmpty()
            val clusters = clusterEmbeddings(groupId, embeddings, clusterCount)

            _uiState.value = _uiState.value.copy(
                group = groupResult.getOrNull(),
                relationshipGraph = relationshipGraph,
                clusters = clusters,
                isLoading = false
            )
        }
    }

    fun saveClusterAsGroup(
        cluster: ClusterGroup,
        name: String,
        description: String?,
        iconType: String?,
        creatorId: String,
        onComplete: (Result<String>) -> Unit
    ) {
        val parentGroup = _uiState.value.group
        val tags = parentGroup?.tags?.map { it.name } ?: emptyList()
        val region = parentGroup?.region?.takeIf { it.isNotBlank() }
        val defaultIcon = parentGroup?.iconType ?: "users"
        val imageUrl = parentGroup?.imageUrl?.takeIf { it.isNotBlank() }
        val finalDescription = description?.takeIf { it.isNotBlank() }
            ?: parentGroup?.description?.takeIf { it.isNotBlank() }
            ?: "소그룹 ${cluster.id + 1}"
        val isPublic = parentGroup?.isPublic ?: true

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "그룹 이름을 입력해주세요"
            )
            onComplete(Result.failure(IllegalArgumentException("그룹 이름이 필요합니다")))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingCluster = true,
                errorMessage = ""
            )
            val result = createGroupUseCase(
                name = name,
                description = finalDescription,
                iconType = iconType ?: defaultIcon,
                tags = tags,
                region = region,
                imageUrl = imageUrl,
                isPublic = isPublic,
                userId = creatorId
            )

        result.onSuccess { group ->
            cluster.members.map { it.userId }
                .distinct()
                .filter { it.isNotBlank() && it != creatorId }
                .forEach { memberId ->
                    when (backendRepository.addGroupMember(group.id, memberId)) {
                        is ApiResult.Error -> {
                            // best effort; ignore for now
                        }
                        else -> Unit
                    }
                }
            _uiState.value = _uiState.value.copy(isSavingCluster = false)
            onComplete(Result.success(group.id))
        }

            result.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSavingCluster = false,
                    errorMessage = throwable.message ?: "그룹 저장에 실패했습니다"
                )
                onComplete(Result.failure(throwable))
            }
        }
    }

    fun updateClusterName(id: Int, name: String) {
        val currentNames = _uiState.value.customClusterNames.toMutableMap()
        currentNames[id] = name
        _uiState.value = _uiState.value.copy(customClusterNames = currentNames)
    }

    fun updateClusterCount(count: Int) {
        val normalized = count.coerceAtLeast(2)
        if (normalized == _uiState.value.clusterCount) return
        val graph = _uiState.value.relationshipGraph
        val embeddings = graph?.embeddings?.values?.toList().orEmpty()
        val clusters = if (graph != null) {
            clusterEmbeddings(graph.groupId, embeddings, normalized)
        } else {
            emptyList()
        }
        _uiState.value = _uiState.value.copy(
            clusterCount = normalized,
            clusters = clusters,
            subgroupRooms = emptyMap()
        )
    }

    fun enterSubgroupRoom(parentGroupId: String, clusterIndex: Int) {
        val cached = _uiState.value.subgroupRooms[clusterIndex]
        if (cached != null) {
            _uiState.value = _uiState.value.copy(enterRoom = cached)
            return
        }
        val clusters = _uiState.value.clusters
        if (clusters.isEmpty()) return

        val requestClusters = clusters.map { cluster ->
            SubgroupClusterRequest(
                index = cluster.id,
                memberIds = cluster.members.map { it.userId }
            )
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRoom = true)
            when (val result = backendRepository.createSubgroups(parentGroupId, requestClusters)) {
                is ApiResult.Success -> {
                    val rooms = result.data.associateBy(
                        { it.clusterIndex },
                        { it.toRoom() }
                    )
                    val target = rooms[clusterIndex]
                    _uiState.value = _uiState.value.copy(
                        subgroupRooms = rooms,
                        enterRoom = target,
                        isCreatingRoom = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRoom = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun resetEnterRoom() {
        _uiState.value = _uiState.value.copy(enterRoom = null)
    }

    private data class EmbeddingItem(
        val user: UserEmbedding,
        val vector: FloatArray
    )

    private fun clusterEmbeddings(
        seedKey: String,
        users: List<UserEmbedding>,
        targetClusterCount: Int
    ): List<ClusterGroup> {
        if (targetClusterCount <= 0) return emptyList()
        if (users.isEmpty()) {
            return List(targetClusterCount) { ClusterGroup(it, emptyList()) }
        }

        val items = users.map { user ->
            EmbeddingItem(user, normalize(user.embeddingVector))
        }

        val clusterCount = minOf(targetClusterCount, items.size)
        val centroids = runKMeans(seedKey, items, clusterCount)
        val assignments = assignBalanced(items, centroids, clusterCount)

        val result = MutableList(targetClusterCount) { ClusterGroup(it, emptyList()) }
        assignments.forEachIndexed { idx, members ->
            result[idx] = ClusterGroup(
                id = idx,
                members = members.map { it.user }.sortedBy { it.userName }
            )
        }
        return result
    }

    private fun runKMeans(
        seedKey: String,
        items: List<EmbeddingItem>,
        k: Int
    ): List<FloatArray> {
        val random = Random(seedKey.hashCode())
        val centroids = mutableListOf<FloatArray>()
        centroids.add(items[random.nextInt(items.size)].vector.copyOf())

        while (centroids.size < k) {
            val distances = items.map { item -> minDistance(item.vector, centroids) }
            val sum = distances.sum()
            val target = random.nextFloat() * sum
            var cumulative = 0f
            var chosenIndex = 0
            for (index in distances.indices) {
                cumulative += distances[index]
                if (cumulative >= target) {
                    chosenIndex = index
                    break
                }
            }
            centroids.add(items[chosenIndex].vector.copyOf())
        }

        repeat(8) {
            val assignments = assignToNearest(items, centroids)
            for (index in 0 until k) {
                val clusterItems = assignments[index]
                if (clusterItems.isNotEmpty()) {
                    centroids[index] = normalize(meanVector(clusterItems.map { it.vector }))
                }
            }
        }

        return centroids
    }

    private fun assignBalanced(
        items: List<EmbeddingItem>,
        centroids: List<FloatArray>,
        k: Int
    ): List<MutableList<EmbeddingItem>> {
        val total = items.size
        val baseSize = total / k
        val remainder = total % k
        val capacities = IntArray(k) { baseSize + if (it < remainder) 1 else 0 }
        val assigned = List(k) { mutableListOf<EmbeddingItem>() }

        data class ScoredItem(
            val item: EmbeddingItem,
            val orderedCentroids: List<Int>,
            val margin: Float
        )

        val scored = items.map { item ->
            val distances = centroids.map { cosineDistance(item.vector, it) }
            val ordered = distances.mapIndexed { index, value -> index to value }
                .sortedBy { it.second }
            val margin = if (ordered.size > 1) ordered[1].second - ordered[0].second else 1f
            ScoredItem(
                item = item,
                orderedCentroids = ordered.map { it.first },
                margin = margin
            )
        }.sortedByDescending { it.margin }

        for (entry in scored) {
            var placed = false
            for (centroidIndex in entry.orderedCentroids) {
                if (assigned[centroidIndex].size < capacities[centroidIndex]) {
                    assigned[centroidIndex].add(entry.item)
                    placed = true
                    break
                }
            }
            if (!placed) {
                val fallbackIndex = assigned.indices.minByOrNull { assigned[it].size } ?: 0
                assigned[fallbackIndex].add(entry.item)
            }
        }

        return assigned.toList()
    }

    private fun assignToNearest(
        items: List<EmbeddingItem>,
        centroids: List<FloatArray>
    ): List<MutableList<EmbeddingItem>> {
        val assigned = List(centroids.size) { mutableListOf<EmbeddingItem>() }
        for (item in items) {
            val index = nearestCentroidIndex(item.vector, centroids)
            assigned[index].add(item)
        }
        return assigned.toList()
    }

    private fun nearestCentroidIndex(vector: FloatArray, centroids: List<FloatArray>): Int {
        var bestIndex = 0
        var bestDistance = cosineDistance(vector, centroids[0])
        for (index in 1 until centroids.size) {
            val distance = cosineDistance(vector, centroids[index])
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun minDistance(vector: FloatArray, centroids: List<FloatArray>): Float {
        var best = cosineDistance(vector, centroids[0])
        for (index in 1 until centroids.size) {
            val distance = cosineDistance(vector, centroids[index])
            if (distance < best) {
                best = distance
            }
        }
        return best
    }

    private fun meanVector(vectors: List<FloatArray>): FloatArray {
        val size = vectors.firstOrNull()?.size ?: 0
        val result = FloatArray(size)
        if (size == 0) return result
        for (vector in vectors) {
            for (index in vector.indices) {
                result[index] += vector[index]
            }
        }
        for (index in result.indices) {
            result[index] /= vectors.size.toFloat()
        }
        return result
    }

    private fun normalize(vector: List<Float>): FloatArray {
        val array = vector.toFloatArray()
        return normalize(array)
    }

    private fun normalize(vector: FloatArray): FloatArray {
        var sumSquares = 0f
        for (value in vector) {
            sumSquares += value * value
        }
        val norm = sqrt(sumSquares)
        if (norm == 0f) return vector
        for (index in vector.indices) {
            vector[index] /= norm
        }
        return vector
    }

    private fun cosineDistance(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        val size = minOf(a.size, b.size)
        for (index in 0 until size) {
            dot += a[index] * b[index]
            normA += a[index] * a[index]
            normB += b[index] * b[index]
        }
        val denom = sqrt(normA) * sqrt(normB)
        if (denom == 0f) return 1f
        val cosine = dot / denom
        return 1f - cosine
    }

    private fun SubgroupItemResponse.toRoom(): SubgroupRoom {
        return SubgroupRoom(
            id = id,
            name = name,
            clusterIndex = clusterIndex,
            memberCount = memberIds.size
        )
    }
}
