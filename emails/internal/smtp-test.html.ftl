<p>
Congratulations! You have successfully configured the SMTP server.
</p>

<table style="margin-bottom: 10px;">
  <tr>
    <td><strong>Tenant Id:</strong></td>
    <td>${(tenant.id)!"\x2013"}</td>
  </tr>
  <tr>
    <td><strong>Tenant name:</strong></td>
    <td>${(tenant.name)!"\x2013"}</td>
  </tr>
</table>

<table style="margin-bottom: 10px;">
  <tr>
    <td><strong>Host:</strong></td>
    <td>${(tenant.emailConfiguration.host)!"\x2013"}</td>
  </tr>
  <tr>
    <td><strong>Port:</strong></td>
    <td>${(tenant.emailConfiguration.port)!"\x2013"}</td>
  </tr>
  <tr>
    <td><strong>Username:</strong></td>
    <td>${(tenant.emailConfiguration.username)!"\x2013"}</td>
  </tr>
  <tr>
    <td><strong>Security:</strong></td>
    <td>${(tenant.emailConfiguration.security)!"\x2013"}</td>
  </tr>
</table>

[#if tenant.emailConfiguration.properties??]
<strong>Additional SMTP settings:</strong>
<pre style="margin-bottom: 25px; border-left: 2px solid black; padding-left: 5px;">${(tenant.emailConfiguration.properties)!"\x2013"}</pre>
[/#if]

<p>
- FusionAuth
</p>
