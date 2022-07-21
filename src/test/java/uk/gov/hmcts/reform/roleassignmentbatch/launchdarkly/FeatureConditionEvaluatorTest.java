package uk.gov.hmcts.reform.roleassignmentbatch.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignmentbatch.domain.model.enums.FlagsEnum;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureConditionEvaluatorTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    FeatureConditionEvaluator featureConditionEvaluator = mock(FeatureConditionEvaluator.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getPositiveResponseForFlag() {
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        featureConditionEvaluator = new FeatureConditionEvaluator(ldClient, "", "");
        assertTrue(featureConditionEvaluator.isFlagEnabled("am_role_assignment_batch_service",
                FlagsEnum.GET_LD_FLAG.getLabel()));
    }

    @Test
    public void getNegativeResponseForFlag() {
        assertFalse(featureConditionEvaluator.isFlagEnabled("am_role_assignment_batch_service",
                ""));
    }

}
