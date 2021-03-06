# Shorty

A simple URL shortening service.

## Configuration

### Environment Variables

- `PORT` - The local port this application runs on. Default `8090`
- `BASE_URL` - The publicly reachable base url of the service. Default `http://localhost:8090`
- `DATABASE_URL` - The JDBC connection string to the Postgres instance.
- `SESSION_SECRET` - 16 byte secret to encrypt the session data

## Getting started

```sh
# migrate database
./sqitch deploy db:pg://root:root@localhost:5432/shorty

# revert migration
./sqitch revert db:pg://root:root@localhost:5432/shorty

# Fetch Node dependencies
npm ci
# Compile frontend assets
npm run css
```

```sh
# start the clojure repl in the `user` namespace

(dev) # switch to dev namespace

(go) # start the machinery
(reset) # reload all changed namespaces and reload the system
(halt) # stop the system

# if changes are not reflected after evaluation, you can hit `cider-ns-refresh` (CIDER only obviously)
```

## Build Step

## Server

TODO

### Frontend

To build the CSS for [Tailwind](https://tailwindcss.com), invoke the npm build command.

```sh
npm run css # Build for development with all classes available.
npm run css:prod # Build for a production environment with unused classes purged.
```

### Linting

[Install clj-kondo](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) to be available in \$PATH.

```sh
clj-kondo --lint "$(clojure -Spath)"
```

## TODO

- Favicon
- validation
  - check if url behind url exists?
  - Check length of target url
- Deployment
  - Add compression support
- Sessions in Postgres?
