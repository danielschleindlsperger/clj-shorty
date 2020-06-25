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
```

```sh
# start the clojure repl in the `user` namespace

(dev) # switch to dev namespace

(go) # start the machinery

# if changes are not reflected after evaluation, you can hit `cider-ns-refresh` (CIDER only obviously)
```

### Linting

[Install clj-kondo](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) to be available in $PATH.

```sh
clj-kondo --lint "$(clojure -Spath)"
```

*Hint* clj-kondo reports `unresolved symbol` when using `mount`'s `defstate` macro. Circumvent with the usage of `declare`:
```clj
(declare db)
(defstate db
  :start {})
```

## TODO

- Build step for tailwind
- Favicon
- Ring middleware
  - Security
- validation
  - check if url behind url exists?
  - Check length of target url
- Postgres connection pool
- CI
- Deployment
- Sessions in Postgres?

## Features

- Make generated URL copy-able to clipboard
