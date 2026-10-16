
package ua.foxminded.carrestservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    private String name;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Manufacturer manufacturer;
}
