# revolut

## Run
```
java -jar money-transfer-jar/build/libs/money-transfer-jar-all.jar
```

## Test
To create new customer:
```
curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/v1/customer/create -d "{\"type\":\"createCustomer\",\"personalDataHash\":\"R2-D2\"}"
```
Result should be similiar to:
{"entity":{"type":"customer","id":1,"timestamp":1548061902602,"dataHash":"R2-D2","blocked":false},"error":"OK"}

To create new account for existing customer (with customerId = 1):
```
curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/v1/account/create -d "{\"type\":\"createAccount\",\"customerId\":1,\"currency\":\"RUR\"}"
```
Result should be similiar to:
{"entity":{"type":"account","id":1,"timestamp":1548062043929,"currency":"RUR","amount":0.0000,"customerId":1},"error":"OK"}

To transfer money from customer to customer:
```
curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/v1/transfer -d
"{\"type\":\"transfer\",\"senderId\":1,\"receiverId\":2,\"currency\":\"RUR\",\"amount\":100.0000}"
```
Result should be similiar to:
{"entity":{"type":"transfer","id":1,"timestamp":1548062308659,"status":"PENDING","senderId":1,"receiverId":2,"currency":"RUR","amount":100.0000},"error":"OK"}

That means transfer successfully enqueued to process. Transfer service processing all pending transfer transactions periodically. By default period is 100 ms.
