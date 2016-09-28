package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.stream.Stream;

interface ReservationChannels {
    @Input
    MessageChannel input();
}

@EnableBinding(ReservationChannels.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}




@RestController
@RefreshScope
class MessageRestController {
	private final String msg;

	@Autowired
	public MessageRestController(@Value("${message}") String msg) {
		this.msg = msg;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message")
	String read() {
		return msg;
	}
}

@MessageEndpoint
class ReservationProcessor {

    private final ReservationRepository reservationRepository;

    @ServiceActivator(inputChannel = "input")
    public void onMessage(Message<String> msg) {
        this.reservationRepository.save(new Reservation(msg.getPayload()));
    }

    @Autowired
    public ReservationProcessor(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
}

@Component
class SampleDataCLR implements CommandLineRunner {

	private final ReservationRepository reservationRepository;

	@Autowired
	public SampleDataCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... strings) throws Exception {
		Stream.of("Josh", "Juergen", "Andrew", "Bridget", "Onsi", "Phil", "Stephane")
				.forEach(name -> reservationRepository.save(new Reservation(name)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}


@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @RestResource(path = "by-name")
    Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
class Reservation {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "reservation_name")
	private String reservationName; //reservation_name

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	public Reservation() {
	}

	public Reservation(String reservationName) {

		this.reservationName = reservationName;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}


}
