package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The UserRepository.
 * @author hp11
 */
@Repository
public interface UserRepository extends BaseOclRepository<UserProfile> {
    List<UserProfile> findByUsernameIs(String username);
}
