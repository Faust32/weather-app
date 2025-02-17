package ru.faust.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "locations", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "latitude", "longitude"})})
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "locations")
    private List<User> usersId;

    @Column(nullable = false)
    @JsonProperty("lat")
    private Double latitude;

    @Column(nullable = false)
    @JsonProperty("lon")
    private Double longitude;

}
