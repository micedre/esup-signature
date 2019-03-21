// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.esupportail.esupsignature.domain;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.esupportail.esupsignature.domain.SignRequest;

privileged aspect SignRequest_Roo_Finder {
    
    public static Long SignRequest.countFindSignRequestsByCreateByEquals(String createBy) {
        if (createBy == null || createBy.length() == 0) throw new IllegalArgumentException("The createBy argument is required");
        EntityManager em = SignRequest.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM SignRequest AS o WHERE o.createBy = :createBy", Long.class);
        q.setParameter("createBy", createBy);
        return ((Long) q.getSingleResult());
    }
    
    public static Long SignRequest.countFindSignRequestsByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = SignRequest.entityManager();
        TypedQuery q = em.createQuery("SELECT COUNT(o) FROM SignRequest AS o WHERE o.name = :name", Long.class);
        q.setParameter("name", name);
        return ((Long) q.getSingleResult());
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByCreateByAndStatusEquals(String createBy, SignRequestStatus status) {
        if (createBy == null || createBy.length() == 0) throw new IllegalArgumentException("The createBy argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = SignRequest.entityManager();
        TypedQuery<SignRequest> q = em.createQuery("SELECT o FROM SignRequest AS o WHERE o.createBy = :createBy AND o.status = :status", SignRequest.class);
        q.setParameter("createBy", createBy);
        q.setParameter("status", status);
        return q;
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByCreateByAndStatusEquals(String createBy, SignRequestStatus status, String sortFieldName, String sortOrder) {
        if (createBy == null || createBy.length() == 0) throw new IllegalArgumentException("The createBy argument is required");
        if (status == null) throw new IllegalArgumentException("The status argument is required");
        EntityManager em = SignRequest.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM SignRequest AS o WHERE o.createBy = :createBy AND o.status = :status");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<SignRequest> q = em.createQuery(queryBuilder.toString(), SignRequest.class);
        q.setParameter("createBy", createBy);
        q.setParameter("status", status);
        return q;
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByCreateByEquals(String createBy) {
        if (createBy == null || createBy.length() == 0) throw new IllegalArgumentException("The createBy argument is required");
        EntityManager em = SignRequest.entityManager();
        TypedQuery<SignRequest> q = em.createQuery("SELECT o FROM SignRequest AS o WHERE o.createBy = :createBy", SignRequest.class);
        q.setParameter("createBy", createBy);
        return q;
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByCreateByEquals(String createBy, String sortFieldName, String sortOrder) {
        if (createBy == null || createBy.length() == 0) throw new IllegalArgumentException("The createBy argument is required");
        EntityManager em = SignRequest.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM SignRequest AS o WHERE o.createBy = :createBy");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<SignRequest> q = em.createQuery(queryBuilder.toString(), SignRequest.class);
        q.setParameter("createBy", createBy);
        return q;
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByNameEquals(String name) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = SignRequest.entityManager();
        TypedQuery<SignRequest> q = em.createQuery("SELECT o FROM SignRequest AS o WHERE o.name = :name", SignRequest.class);
        q.setParameter("name", name);
        return q;
    }
    
    public static TypedQuery<SignRequest> SignRequest.findSignRequestsByNameEquals(String name, String sortFieldName, String sortOrder) {
        if (name == null || name.length() == 0) throw new IllegalArgumentException("The name argument is required");
        EntityManager em = SignRequest.entityManager();
        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM SignRequest AS o WHERE o.name = :name");
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            queryBuilder.append(" ORDER BY ").append(sortFieldName);
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                queryBuilder.append(" ").append(sortOrder);
            }
        }
        TypedQuery<SignRequest> q = em.createQuery(queryBuilder.toString(), SignRequest.class);
        q.setParameter("name", name);
        return q;
    }
    
}
