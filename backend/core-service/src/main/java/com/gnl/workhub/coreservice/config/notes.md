### Spring security workflow
 - A request hits the SecurityConfig (The Gate).
 - The Gate sees it needs authentication and calls the AuthenticationManager (The Manager).
 - The Manager asks the AuthenticationProvider (The Judge) to check the credentials.
 - The Judge uses the UserDetailsService (The Searcher) to find the user in Postgres.
 - The Judge uses the PasswordEncoder (The Scrambler) to verify the password.
 - If all good, the Gate opens.

#### The Login Flow (Uses PasswordEncoder and JwtService):
User sends Email/Password.
AuthenticationManager -> Provider -> UserDetailsService -> PasswordEncoder.
Success? Call JwtService.generateToken().
Return Token to User.

#### The Authenticated Flow (Uses JwtFilter & JwtService):
User sends a Request with Authorization: Bearer <TOKEN>.
JwtFilter intercepts the request.
JwtService checks if the token is valid.
If valid, the JwtFilter tells Spring to allow the request.
The request goes to your ProjectController.

#### How jwt checks if user is valid
generateToken: Takes a User and packages their email into a JSON string, then signs it with your secretKey.
extractUsername: Decodes the token to see who it belongs to.
isTokenValid: Checks the signature. If a user tried to change the email inside the token, the signature wouldn't match your secretKey, and the Guard (this service) would reject it immediately.