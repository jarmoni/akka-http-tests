## Cert-Gen

See: <http://typesafehub.github.io/ssl-config/CertificateGeneration.html>

### Create a server CA

```
#!/bin/bash

export PW="s3cr3t"

# Create a self signed key pair root CA certificate.
keytool -genkeypair -v \
  -alias exampleca \
  -dname "CN=exampleCA, OU=Example Org, O=Example Company, L=San Francisco, ST=California, C=US" \
  -keystore exampleca.jks \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 4096 \
  -ext KeyUsage:critical="keyCertSign" \
  -ext BasicConstraints:critical="ca:true" \
  -validity 99999

# Export the exampleCA public certificate as exampleca.crt so that it can be used in trust stores.
keytool -export -v \
  -alias exampleca \
  -file exampleca.crt \
  -keypass:env PW \
  -storepass:env PW \
  -keystore exampleca.jks \
  -rfc
```

### Generating example.com certificates

```
#!/bin/bash

export PW="s3cr3t"

# Create a server certificate, tied to example.com
keytool -genkeypair -v \
  -alias example.com \
  -dname "CN=example.com, OU=Example Org, O=Example Company, L=San Francisco, ST=California, C=US" \
  -keystore example.com.jks \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 2048 \
  -validity 99999

# Create a certificate signing request for example.com
keytool -certreq -v \
  -alias example.com \
  -keypass:env PW \
  -storepass:env PW \
  -keystore example.com.jks \
  -file example.com.csr

# Tell exampleCA to sign the example.com certificate. Note the extension is on the request, not the
# original certificate.
# Technically, keyUsage should be digitalSignature for DHE or ECDHE, keyEncipherment for RSA.
keytool -gencert -v \
  -alias exampleca \
  -keypass:env PW \
  -storepass:env PW \
  -keystore exampleca.jks \
  -infile example.com.csr \
  -outfile example.com.crt \
  -ext KeyUsage:critical="digitalSignature,keyEncipherment" \
  -ext EKU="serverAuth" \
  -ext SAN="DNS:example.com" \
  -rfc

# Tell example.com.jks it can trust exampleca as a signer.
keytool -import -v \
  -alias exampleca \
  -file exampleca.crt \
  -keystore example.com.jks \
  -storetype JKS \
  -storepass:env PW << EOF
yes
EOF

# Import the signed certificate back into example.com.jks 
keytool -import -v \
  -alias example.com \
  -file example.com.crt \
  -keystore example.com.jks \
  -storetype JKS \
  -storepass:env PW

# List out the contents of example.com.jks just to confirm it.  
# If you are using Play as a TLS termination point, this is the key store you should present as the server.
keytool -list -v \
  -keystore example.com.jks \
  -storepass:env PW
```
