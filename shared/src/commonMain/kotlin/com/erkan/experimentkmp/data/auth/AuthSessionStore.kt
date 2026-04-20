package com.erkan.experimentkmp.data.auth

interface AuthSessionStore {
    fun save(session: StoredAuthSession)

    fun load(): StoredAuthSession?

    fun clear()
}
