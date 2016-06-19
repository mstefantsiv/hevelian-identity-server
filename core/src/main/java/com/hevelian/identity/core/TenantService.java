package com.hevelian.identity.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hevelian.identity.core.exc.EntityNotFoundByCriteriaException;
import com.hevelian.identity.core.model.Tenant;
import com.hevelian.identity.core.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
// Manage all transactions in service layer, where business logic occurs.
@Transactional(readOnly = true)
@Secured(value = SystemRoles.SUPER_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Autowired) )
public class TenantService {
    // TODO review the regexp
    public static final String DOMAIN_REGEXP = "\\w+\\.\\w+";
    private final TenantRepository tenantRepository;

    @Transactional
    public void activateTenant(String tenantDomain) throws TenantNotFoundByDomainException {
        int affectedRows = tenantRepository.setActive(tenantDomain, true);
        if (affectedRows == 0) {
            throw new TenantNotFoundByDomainException(tenantDomain);
        }
    }

    @Transactional
    public void deactivateTenant(String tenantDomain) throws TenantNotFoundByDomainException {
        int affectedRows = tenantRepository.setActive(tenantDomain, false);
        if (affectedRows == 0) {
            throw new TenantNotFoundByDomainException(tenantDomain);
        }
    }

    @Transactional
    public Tenant addTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deleteTenant(String tenantDomain) throws TenantNotFoundByDomainException {
        int affectedRows = tenantRepository.deleteByDomain(tenantDomain);
        if (affectedRows == 0) {
            throw new TenantNotFoundByDomainException(tenantDomain);
        }
    }

    public Tenant getTenant(String tenantDomain) throws TenantNotFoundByDomainException {
        Tenant tenant = tenantRepository.findByDomain(tenantDomain);
        if (tenant == null) {
            throw new TenantNotFoundByDomainException(tenantDomain);
        }
        return tenant;
    }

    @Transactional
    public Tenant updateTenant(Tenant tenant) throws TenantNotFoundByDomainException {
        Tenant tenantEntity = getTenant(tenant.getDomain());
        tenantEntity.setActive(tenant.getActive());
        tenantEntity.getTenantAdmin().setName(tenant.getTenantAdmin().getName());
        if (tenant.getTenantAdmin().getPassword() != null) {
            tenantEntity.getTenantAdmin().setPassword(tenant.getTenantAdmin().getPassword());
        }
        return tenantRepository.save(tenantEntity);
    }

    public Iterable<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public static class TenantNotFoundByDomainException extends EntityNotFoundByCriteriaException {
        private static final long serialVersionUID = -3353395068580678695L;

        public TenantNotFoundByDomainException(String tenantDomain) {
            super("tenantDomain", tenantDomain);
        }
    }
}
