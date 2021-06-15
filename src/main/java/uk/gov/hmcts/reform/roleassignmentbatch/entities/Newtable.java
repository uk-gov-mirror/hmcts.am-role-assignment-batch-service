package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Builder
@Table(name = "nitish_table" )
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Newtable implements Serializable {
    @Id
    @Column(name = "myid")
    private String id;
}
