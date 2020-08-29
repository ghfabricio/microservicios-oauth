package com.fabriciospringcloud.microservicios.app.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/* Este microservicio de encarga de generar el token. Utiliza las dependencias Eureka Client de Spring Cloud Discovery,
 * Cloud Auth2 de Spring Cloud Secutity, Dev Tools de Developer Tool y Spring Web Started de Web y Open Feign de Spring 
 * Cloud Routing.
 * Vamos a tener un cliente Feign que se comunica con el microservicio usuaris para obtener la autenticacion mediante
 * el username.
 * Ademas tenemos qu agregar la dependencia de usuario commons excluyendo la dependencia JPA:
		<dependency>
			<groupId>com.fabriciospringcloud.microservicios.app.commons.usuarios</groupId>
			<artifactId>microservicios-commons-usuarios</artifactId>
			<version>0.0.1-SNAPSHOT</version>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-data-jpa</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
 * 
 * Luego habilitamos Eureca Client con la anotacion @EnableEurekaClient.
 * Con @EnableFeignClients habilitamos el cliente http y luego creamos una package para implementar u interface.
 * 
 */
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
@EntityScan({"com.fabriciospringcloud.microservicios.app.commons.usuarios.models.entity"})
public class MicroserviciosServicioOauthApplication implements CommandLineRunner{

	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	public static void main(String[] args) {
		SpringApplication.run(MicroserviciosServicioOauthApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		String password = "12345";
		
		for (int i = 0; i < 4; i++) {
			String passwordBCrypt = passwordEncode.encode(password);
			System.out.println(passwordBCrypt);
		}
		
	}

}