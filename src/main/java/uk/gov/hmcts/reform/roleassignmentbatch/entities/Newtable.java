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
import lombok.ToString;

@Entity
@Builder
@Table(name = "nitish_table")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Newtable implements Serializable {
    @Id
    @Column(name = "myid")
    private String myid;

    @Column(name = "column2")
    private String column2;
}
