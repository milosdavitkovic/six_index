# SAP Marketing DocStore

DocStore accepts SAP Marketing document requests and delivers email, PDF, SMS, and print flows.

Main integrations are SAP Marketing, AWS S3, Kafka, OpenShift, and API Management.
The service uses Java 21, Spring Boot 3, Maven, Helm, and Tekton.

Priorities, in order:

1. Data integrity
2. Reliability
3. Backward compatibility
4. Observability

Never propose a breaking data migration without an explicit migration plan and compatibility period.
