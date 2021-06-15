package uk.gov.hmcts.reform.roleassignmentbatch.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name = "nitish_table" )
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Newtable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "myid")
    private Long id;

    @Column(name = "column2")
    private String column2;
}
