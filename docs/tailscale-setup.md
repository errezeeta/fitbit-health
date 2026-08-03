# Tailscale setup

The Android client reaches the private gateway through the same Tailscale tailnet as the Hermes PC.

## PC

1. Install and sign in to Tailscale.
2. Set local environment variables from `gateway/.env.example`.
3. Start the gateway on port `8844`.
4. Confirm `http://<pc-tailscale-hostname>:8844/health` is reachable from the tailnet.
5. Restrict the Windows Firewall inbound rule to the Tailscale adapter/profile.

## Android

1. Install the official Tailscale Android app and sign in to the same tailnet.
2. Enable the VPN connection.
3. In Fitbit Health settings, enter the PC's Tailscale hostname or `100.x.y.z` address plus port `8844`.
4. Enter the gateway token and use “Probar conexión”.

Do not commit the hostname, IP, token, or any personal ACL file. The public app only stores a user-provided server URL and encrypted token locally.

Tailscale encrypts traffic within the tailnet. HTTPS can be added later for stronger defense-in-depth and to avoid Android cleartext restrictions.
