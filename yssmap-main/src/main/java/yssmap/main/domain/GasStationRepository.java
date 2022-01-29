package yssmap.main.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GasStationRepository extends JpaRepository<GasStation, String> {

	@Query("select g from GasStation g "+
	"where g.latitude >=:bottomLatitude and g.latitude<=:topLatitude "+
	"and g.longitude>=:leftLongitude and g.longitude<=:rightLongitude "+
	"and g.deletedAt is null")
	List<GasStation> findAllInBoundary(@Param("bottomLatitude") Double bottomLatitude,
		@Param("topLatitude") Double topLatitude, @Param("leftLongitude") Double leftLongitude,
		@Param("rightLongitude") Double rightLongitude);

	Optional<GasStation> findByStationCodeAndDeletedAtIsNull(String stationCode);

	List<GasStation> findAllByDeletedAtIsNull();
	Page<GasStation> findAllByDeletedAtIsNull(Pageable pageable);
}
