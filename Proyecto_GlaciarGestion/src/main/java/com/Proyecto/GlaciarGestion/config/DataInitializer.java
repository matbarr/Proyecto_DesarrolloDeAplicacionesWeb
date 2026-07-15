package com.Proyecto.GlaciarGestion.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Proyecto.GlaciarGestion.model.Producto;
import com.Proyecto.GlaciarGestion.model.RolUsuario;
import com.Proyecto.GlaciarGestion.model.Usuario;
import com.Proyecto.GlaciarGestion.repository.ProductoRepository;
import com.Proyecto.GlaciarGestion.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        return args -> {
            if (!usuarioRepository.existsByCorreo("admin@glaciar.com")) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setCorreo("admin@glaciar.com");
                admin.setTelefono("88888888");
                admin.setContrasena("admin123");
                admin.setRol(RolUsuario.ADMINISTRADOR);
                admin.setActivo(true);
                usuarioRepository.save(admin);
            }

            if (productoRepository.count() == 0) {
                productoRepository.save(crearProducto("Bolsa de hielo pequena", "2 kg", new BigDecimal("1200.00"), 200));
                productoRepository.save(crearProducto("Bolsa de hielo mediana", "5 kg", new BigDecimal("2500.00"), 120));
                productoRepository.save(crearProducto("Bolsa de hielo grande", "10 kg", new BigDecimal("4500.00"), 80));
            }
        };
    }

    private Producto crearProducto(String nombre, String presentacion, BigDecimal precio, int cantidad) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPresentacion(presentacion);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setActivo(true);
        return producto;
    }
}
