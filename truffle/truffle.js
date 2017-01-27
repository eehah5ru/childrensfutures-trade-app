module.exports = {
  build: {
    "index.html": "index.html",
    "app.js": [
      "javascripts/app.js"
    ],
    "app.css": [
      "stylesheets/app.css"
    ],
    "images/": "images/"
  },

  networks: {
    ropsten: {
      host: "localhost",
      port: 8546,
      network_id: "*", // Match any network id
      from: "0x77d24Aae19C76C4e5a943776425Eef4D73AaF971"
    },
    dev: {
      host: "localhost",
      port: 8545

    }
  }
};
