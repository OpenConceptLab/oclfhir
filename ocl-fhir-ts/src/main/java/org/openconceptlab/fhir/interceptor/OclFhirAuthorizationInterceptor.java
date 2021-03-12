package org.openconceptlab.fhir.interceptor;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The OclFhirAuthorizationInterceptor class.
 * @author harpatel1
 */
@Component
public class OclFhirAuthorizationInterceptor extends AuthorizationInterceptor {

   @Override
   public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
      RuleBuilder builder = getDefaultRuleBuilder();
      return builder.build();
   }

   private RuleBuilder getDefaultRuleBuilder() {
      RuleBuilder ruleBuilder = new RuleBuilder();
      ruleBuilder
              .allow().metadata().andThen()
              .allow().read().allResources().withAnyId().andThen()
              .allow().write().resourcesOfType(CodeSystem.class).withAnyId().andThen()
              .allow().delete().resourcesOfType(CodeSystem.class).withAnyId().andThen()
              .allow().write().resourcesOfType(ValueSet.class).withAnyId().andThen()
              .allow().delete().resourcesOfType(ValueSet.class).withAnyId().andThen()
              .allow().write().resourcesOfType(ConceptMap.class).withAnyId().andThen()
              .allow().delete().resourcesOfType(ConceptMap.class).withAnyId().andThen()
              .allow().operation().withAnyName().atAnyLevel().andAllowAllResponses();
      return ruleBuilder;
   }

}
