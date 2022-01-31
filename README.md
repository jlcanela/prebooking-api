# Prebooking API

Entrance in some amusement parks is conditionned by prebooking. 

Let’s develop a simple API to enable prebooking for a given day. 

## Run API

```
./mill prebookingapi.run serve
```

## Use cases

**As a tourist a want to prebook n tickets for a given day yyyy/mm/dd.**

Positive scenario
* Given a selected day and n tickets
* And n tickets are available
* When I request a prebook
* Then I get a prebook confirmation for n tickets
* And an audit file log is written

Negative scenario
* Given a selected day and n tickets
* And n tickets are not available
* When I request a prebook
* Then I get a prebook refusal for n tickets
* And an audit file log is written

Performance requirement
* a prebooking node can cope with 10000 requests per second

## API

To request a prebook
```
curl -d '{"day":"2022/01/01", "user":1234}' -H "Content-Type: application/json" -X POST http://localhost:8090/prebook
```

## Performance

Naive Performance measurement:
```
$ wrk -t5 -c50 -s test/prebook.lua http://localhost:8090/prebook
Running 10s test @ http://localhost:8090/prebook
  5 threads and 50 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     9.24ms   35.85ms 384.79ms   96.14%
    Req/Sec     5.76k     2.37k   10.62k    71.34%
  278450 requests in 10.04s, 21.40MB read
Requests/sec:  27732.39
Transfer/sec:      2.13MB
```

Note: after some ramp-up I get ~ 36000 Requests/sec with 8 cores / 16 threads

Ensure counter have been incremented after multiple test sessions:
```
curl http://localhost:8090/prebook
```
