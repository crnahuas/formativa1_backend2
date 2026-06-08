package com.minimarket.security.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Inicializar Roles
        Rol rolCliente = getOrCreateRol(SecurityRoles.ROLE_CLIENTE);
        Rol rolEmpleado = getOrCreateRol(SecurityRoles.ROLE_EMPLEADO);
        Rol rolGerente = getOrCreateRol(SecurityRoles.ROLE_GERENTE);

        // 2. Inicializar Usuarios
        if (!usuarioRepository.findByUsername("gerente").isPresent()) {
            Usuario gerente = new Usuario();
            gerente.setUsername("gerente");
            gerente.setPassword(passwordEncoder.encode("gerente123"));
            Set<Rol> roles = new HashSet<>();
            roles.add(rolGerente);
            gerente.setRoles(roles);
            usuarioRepository.save(gerente);
            logger.info("Usuario 'gerente' creado exitosamente con contrasena BCrypt.");
        }

        if (!usuarioRepository.findByUsername("empleado").isPresent()) {
            Usuario empleado = new Usuario();
            empleado.setUsername("empleado");
            empleado.setPassword(passwordEncoder.encode("empleado123"));
            Set<Rol> roles = new HashSet<>();
            roles.add(rolEmpleado);
            empleado.setRoles(roles);
            usuarioRepository.save(empleado);
            logger.info("Usuario 'empleado' creado exitosamente con contrasena BCrypt.");
        }

        if (!usuarioRepository.findByUsername("cliente").isPresent()) {
            Usuario cliente = new Usuario();
            cliente.setUsername("cliente");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            Set<Rol> roles = new HashSet<>();
            roles.add(rolCliente);
            cliente.setRoles(roles);
            usuarioRepository.save(cliente);
            logger.info("Usuario 'cliente' creado exitosamente con contrasena BCrypt.");
        }

        // 3. Inicializar Categorías y Productos para pruebas de endpoints
        if (categoriaRepository.count() == 0) {
            Categoria abarrotes = new Categoria();
            abarrotes.setNombre("Abarrotes");
            abarrotes = categoriaRepository.save(abarrotes);

            Categoria lacteos = new Categoria();
            lacteos.setNombre("Lácteos");
            lacteos = categoriaRepository.save(lacteos);

            Categoria bebidas = new Categoria();
            bebidas.setNombre("Bebidas");
            bebidas = categoriaRepository.save(bebidas);

            // Crear productos
            Producto arroz = new Producto();
            arroz.setNombre("Arroz Grano Largo 1kg");
            arroz.setPrecio(1500.0);
            arroz.setStock(50);
            arroz.setCategoria(abarrotes);
            productoRepository.save(arroz);

            Producto leche = new Producto();
            leche.setNombre("Leche Entera 1L");
            leche.setPrecio(1100.0);
            leche.setStock(100);
            leche.setCategoria(lacteos);
            productoRepository.save(leche);

            Producto bebidaCola = new Producto();
            bebidaCola.setNombre("Bebida Cola 2.5L");
            bebidaCola.setPrecio(2200.0);
            bebidaCola.setStock(80);
            bebidaCola.setCategoria(bebidas);
            productoRepository.save(bebidaCola);

            logger.info("Categorias y productos de prueba inicializados.");
        }
    }

    private Rol getOrCreateRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Rol newRol = new Rol();
                    newRol.setNombre(nombre);
                    return rolRepository.save(newRol);
                });
    }
}
