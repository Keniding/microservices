package com.dipierplus.carts.model;

public enum CartStatus {
    ACTIVE,      // Carrito activo, en uso
    ARCHIVED,    // Carrito archivado, no en uso
    COMPLETED,   // Carrito completado, proceso de compra finalizado
    EXPIRED;     // Carrito expirado, ya no es válido
}
