package org.example.saytoreverse.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private BusinessType type;

    private String location;

    private float rating;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
