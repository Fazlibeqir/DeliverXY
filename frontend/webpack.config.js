const webpack = require("@nativescript/webpack");
const dotenv = require("dotenv");

// Load environment variables from .env file
dotenv.config();

module.exports = (env) => {
	webpack.init(env);

	const config = webpack.resolveConfig();
	
	// Inject environment variables
	if (config.plugins) {
		const DefinePlugin = require("webpack").DefinePlugin;
		config.plugins.push(
			new DefinePlugin({
				"process.env.MAPBOX_ACCESS_TOKEN": JSON.stringify(process.env.MAPBOX_ACCESS_TOKEN || ""),
				"process.env.API_URL": JSON.stringify(process.env.API_URL || "http://localhost:8080"),
			})
		);
	}

	return config;
};
