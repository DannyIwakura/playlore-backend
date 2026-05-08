package com.playrole.specification;

import com.playrole.model.PerfilPersonaje;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class PerfilPersonajeSpec {

    public static Specification<PerfilPersonaje> filtrar(
            String nombre, String genero, String raza, String clase,
            Integer edadMin, Integer edadMax, Integer categoriaId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            //vamos aplicando el if segun el filtro activo
            if (nombre != null && !nombre.isBlank())
                predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));

            if (genero != null && !genero.isBlank())
                predicates.add(cb.equal(cb.lower(root.get("genero")), genero.toLowerCase()));

            if (raza != null && !raza.isBlank())
                predicates.add(cb.like(cb.lower(root.get("raza")), "%" + raza.toLowerCase() + "%"));

            if (clase != null && !clase.isBlank())
                predicates.add(cb.like(cb.lower(root.get("clase")), "%" + clase.toLowerCase() + "%"));

            if (edadMin != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("edadPersonaje"), edadMin));

            if (edadMax != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("edadPersonaje"), edadMax));

            if (categoriaId != null) {
                Join<Object, Object> categorias = root.join("personajeCategoriaList", JoinType.INNER);
                predicates.add(cb.equal(categorias.get("idCategoria").get("idCategoria"), categoriaId));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}