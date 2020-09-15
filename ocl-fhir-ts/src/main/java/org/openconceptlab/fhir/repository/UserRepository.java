package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.UserProfile;
import org.springframework.stereotype.Repository;

/**
 * The UserRepository.
 * @author hp11
 */
@Repository
public interface UserRepository extends BaseOclRepository<UserProfile> {
}
