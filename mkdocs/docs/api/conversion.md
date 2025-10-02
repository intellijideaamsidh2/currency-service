# Conversion API

## Convert

```http
GET /currencyconversion/{from}/to/{to}/quantity/{qty}
```

Response

```json
{
  "fromCurrency":"USD",
  "toCurrency":"INR",
  "quantity":100,
  "rate":83.50,
  "convertedAmount":8350.00
}
```
