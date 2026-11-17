package com.example.empresamensajeria.data.repository

import com.example.empresamensajeria.data.local.dao.AdministradorDao

class AdminRepository(private val administradorDao: AdministradorDao) {

    suspend fun iniciarSesionAdmin(usuario: String, contrasena: String): Boolean =
        administradorDao.validar(usuario.trim(), contrasena) != null
}
