package org.openconceptlab.fhir.repository;

import org.hl7.fhir.r4.model.Base;
import org.openconceptlab.fhir.model.UserProfilesOrganization;

import java.util.List;

public interface UserProfilesOrganizationRepository extends BaseOclRepository<UserProfilesOrganization> {

    List<UserProfilesOrganization> findByOrganizationMnemonic(String org);
    List<UserProfilesOrganization> findByUserprofileUsername(String username);

}
