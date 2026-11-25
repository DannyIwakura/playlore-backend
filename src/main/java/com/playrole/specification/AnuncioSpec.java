package com.playrole.specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.playrole.enums.EstadoPersonaje;
import com.playrole.model.Anuncio;
import com.playrole.model.AnuncioCategoria;
import com.playrole.model.BaneoGlobal;
import com.playrole.model.PerfilPersonaje;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/** Filtros de BD del listado de anuncios. La presencia se aplica en el service. */
public final class AnuncioSpec {

    private static final char LIKE_ESCAPE = '\\';

    private AnuncioSpec() {
    }

    public static Specification<Anuncio> filtrar(
            Integer usuarioId, String nombre, String genero, String raza, String clase,
            Integer edadMin, Integer edadMax,
            Collection<Integer> categoriaIds, Collection<Integer> excluirIds) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("activo")));

            Join<Anuncio, PerfilPersonaje> personaje = root.join("personaje", JoinType.INNER);

            // El autor nunca ve sus propios anuncios en el listado público;
            // sus gestionables siguen disponibles en `/anuncios/mios`.
            if (usuarioId != null) {
                predicates.add(cb.notEqual(personaje.get("userId").get("userId"), usuarioId));
            }

            addLike(predicates, cb, personaje.get("nombre"), nombre);
            addLike(predicates, cb, personaje.get("raza"), raza);
            addLike(predicates, cb, personaje.get("clase"), clase);

            if (genero != null && !genero.isBlank()) {
                predicates.add(cb.equal(cb.lower(personaje.get("genero")),
                        genero.toLowerCase(Locale.ROOT)));
            }
            if (edadMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(personaje.get("edadPersonaje"), edadMin));
            }
            if (edadMax != null) {
                predicates.add(cb.lessThanOrEqualTo(personaje.get("edadPersonaje"), edadMax));
            }

            // Varias categorías = OR (pertenece a cualquiera de ellas), sin duplicados.
            if (categoriaIds != null && !categoriaIds.isEmpty()) {
                Join<Anuncio, AnuncioCategoria> categorias =
                        root.join("anuncioCategoriaList", JoinType.INNER);
                predicates.add(categorias.get("idCategoria").get("idCategoria").in(categoriaIds));
                query.distinct(true);
            }

            // Personajes DESACTIVADOS nunca se muestran (estado null se tolera en datos antiguos).
            Path<EstadoPersonaje> estado = personaje.get("estado");
            predicates.add(cb.or(cb.isNull(estado), cb.notEqual(estado, EstadoPersonaje.DESACTIVADO)));

            // Sin baneo global activo: de cuenta (usuario) o del propio personaje.
            predicates.add(cb.not(cb.exists(subconsultaBaneo(query, cb, personaje, true))));
            predicates.add(cb.not(cb.exists(subconsultaBaneo(query, cb, personaje, false))));

            if (excluirIds != null && !excluirIds.isEmpty()) {
                predicates.add(cb.not(root.get("idAnuncio").in(excluirIds)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<Integer> subconsultaBaneo(
            CriteriaQuery<?> query, CriteriaBuilder cb,
            Join<Anuncio, PerfilPersonaje> personaje, boolean porCuenta) {

        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<BaneoGlobal> baneo = sub.from(BaneoGlobal.class);
        sub.select(baneo.get("idBaneoGlobal"));

        Predicate objetivo;
        if (porCuenta) {
            objetivo = cb.and(
                    cb.equal(baneo.get("usuario").get("userId"), personaje.get("userId").get("userId")),
                    baneo.get("personaje").isNull());
        } else {
            objetivo = cb.equal(baneo.get("personaje").get("idPersonaje"),
                    personaje.get("idPersonaje"));
        }

        Path<Date> expiracion = baneo.get("fechaExpiracion");
        Predicate vigente = cb.or(expiracion.isNull(),
                cb.greaterThan(expiracion, new Date()));

        sub.where(cb.and(objetivo, vigente));
        return sub;
    }

    private static void addLike(List<Predicate> predicates, CriteriaBuilder cb,
            Path<String> path, String valor) {
        if (valor != null && !valor.isBlank()) {
            String patron = "%" + valor.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            predicates.add(cb.like(cb.lower(path), patron, LIKE_ESCAPE));
        }
    }
}
