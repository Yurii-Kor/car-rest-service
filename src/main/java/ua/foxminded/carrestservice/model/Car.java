package ua.foxminded.carrestservice.model;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import ua.foxminded.carrestservice.model.converter.YearShortConverter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Convert(converter = YearShortConverter.class)
    private Year year;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CarModel model;

    @Setter(AccessLevel.NONE)
    @ManyToMany
    @JoinTable(
            name = "car_category",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}
