package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.UserProfile;
import org.springframework.stereotype.Repository;

/**
 * The UserRepository.
 * @author harpatel1
 */
@Repository
public interface UserRepository extends BaseOclRepository<UserProfile> {

    UserProfile findByUsername(String username);

}
