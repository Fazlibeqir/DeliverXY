const webpack = require("@nativescript/webpack");
const dotenv = require("dotenv");
const { DefinePlugin } = require("webpack");

// Load environment variables from .env
dotenv.config();

const getRequiredEnv = (key) => {
	const value = process.env[key];

	if (!value || value.trim() === "") {
		throw new Error(`Missing required environment variable: ${key}`);
	}

	return value;
};

module.exports = (env = {}) => {
	webpack.init(env);

	const config = webpack.resolveConfig();

	const isPreview = !!env.preview;

	const MAPBOX_ACCESS_TOKEN = getRequiredEnv("MAPBOX_ACCESS_TOKEN");

	const ANDROID_API_URL =
		process.env.ANDROID_API_URL || "http://13.60.157.179:8080";

	const PREVIEW_API_URL =
		process.env.PREVIEW_API_URL || "https://55c8-92-53-31-13.ngrok-free.app";

	config.plugins = config.plugins || [];

	config.plugins.push(
		new DefinePlugin({
			"process.env.NS_PREVIEW": JSON.stringify(isPreview),
			"process.env.MAPBOX_ACCESS_TOKEN": JSON.stringify(MAPBOX_ACCESS_TOKEN),
			"process.env.ANDROID_API_URL": JSON.stringify(ANDROID_API_URL),
			"process.env.PREVIEW_API_URL": JSON.stringify(PREVIEW_API_URL),
			"process.env.API_URL": JSON.stringify(
				isPreview ? PREVIEW_API_URL : ANDROID_API_URL
			),
		})
	);

	return config;
};