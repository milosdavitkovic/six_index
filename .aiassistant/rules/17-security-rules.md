# Security Rules

- Treat HTA+, URL, HTML, file, and configuration input as untrusted. Validate size, format, allowed values, and business constraints.
- Use `@Valid` DTOs and typed identifiers where the contract allows; validate ownership/authorization through the established APIM/security context.
- Never hardcode credentials. Use the configured secret/Vault/cluster mechanism and IAM roles where available.
- Use least privilege, encrypted transport, and encrypted S3 objects; preserve configured bucket/account separation.
- Generate safe, collision-resistant hot-folder and S3 object names. Do not derive paths directly from user-controlled filenames.
- Allow-list remote hosts before fetching HTML/URLs and prevent SSRF. Validate file content type and size, not only extensions.
- Use parameterized queries if persistence is introduced; do not add JPA/repository assumptions to this service.
- Do not log secrets, tokens, authorization headers, base64 documents, or unnecessary personal data. Sanitize error responses.
- Review dependencies, container images, Helm values, and APIM/security configuration for vulnerabilities before release.

✅ Good

```java
if (!allowedHosts.contains(uri.getHost()) || !"https".equals(uri.getScheme())) {
    throw new ValidationException("HTML source is not allowed");
}
var objectKey = UUID.randomUUID() + ".pdf";
```

❌ Bad

```java
var html = restTemplate.getForObject(request.emailUrl(), String.class);
s3Client.putObject(PutObjectRequest.builder().key(request.fileName()).build(), body);
```

