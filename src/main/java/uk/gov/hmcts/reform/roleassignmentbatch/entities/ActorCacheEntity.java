package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentbatch.config.JsonBConverter;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "actor_cache_control")
public class ActorCacheEntity implements Serializable {

    @Id
    @Column(name = "actor_id", nullable = false)
    private String actorIds;

    @Version
    @Column(name = "etag", nullable = false)
    private long etag;

    @Column(name = "json_response", nullable = true, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private String roleAssignmentResponse;

}

