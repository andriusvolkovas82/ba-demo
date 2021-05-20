Spring Boot app - you know how to run it.

* Create payment

POST http://localhost:8082/payment

Sample JSON of valid payment - {"type":"TYPE1","amount":10,"currency":"EUR","debtorIban":"DBIBAN","creditorIban":"CRIBAN","details":"details","bic":"BIC"}

* Get Ids of all active payments, optionally filter by amount

GET http://localhost:8082/payment/activeIds

GET http://localhost:8082/payment/activeIds?amount={amount}

* Cancel payment

PUT http://localhost:8082/payment/cancel/{id}

* Get payment by Id

GET http://localhost:8082/payment/{id}
