package com.example.madclass01.di

import com.example.madclass01.data.repository.GroupRepositoryImpl
import com.example.madclass01.data.repository.InviteRepositoryImpl
import com.example.madclass01.data.repository.LoginRepositoryImpl
import com.example.madclass01.data.repository.ChatRepositoryImpl
import com.example.madclass01.domain.repository.GroupRepository
import com.example.madclass01.domain.repository.InviteRepository
import com.example.madclass01.domain.repository.LoginRepository
import com.example.madclass01.domain.repository.ChatRepository
import com.example.madclass01.data.repository.GroupDetailRepositoryImpl
import com.example.madclass01.domain.repository.GroupDetailRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository
    
    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository
    
    @Binds
    @Singleton
    abstract fun bindInviteRepository(
        inviteRepositoryImpl: InviteRepositoryImpl
    ): InviteRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindGroupDetailRepository(
        groupDetailRepositoryImpl: GroupDetailRepositoryImpl
    ): GroupDetailRepository
}
