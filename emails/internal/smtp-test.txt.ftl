Congratulations! You have successfully configured the SMTP server.

Tenant Id: ${(tenant.id)!"\x2013"}
Tenant name: ${(tenant.name)!"\x2013"}

Host: ${(tenant.emailConfiguration.host)!"\x2013"}
Port: ${(tenant.emailConfiguration.port)!"\x2013"}
Username: ${(tenant.emailConfiguration.username)!"\x2013"}
Security: ${(tenant.emailConfiguration.security)!"\x2013"}

[#if tenant.emailConfiguration.properties??]
Additional SMTP settings:
[#list tenant.emailConfiguration.properties?split("\n") as property]
${property!"\x2013"}
[/#list]
[/#if]

- FusionAuth