const path = require('path');
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
	entry: './src/index.tsx',
	mode: 'development',
	devServer: {
		static: {
			directory: path.join(__dirname, 'dist'),
		},
		compress: false,
		historyApiFallback: true,
		port: 8000,
		proxy: [
			{
				context: [process.env.API_CONTEXT || '/api'],
				target: process.env.API_TARGET || 'http://localhost:8080',
				onProxyRes: (proxyRes, req, res) => {
					proxyRes.on('close', () => {
						if (!res.writableEnded) {
							res.end();
						}
					});
					res.on('close', () => {
						proxyRes.destroy();
					});
				},
			}
		],
	},
	module: {
		rules: [
			{
				test: /\.tsx?$/,
				use: 'ts-loader',
				exclude: /node_modules/,
			},
			{
				test: /\.css$/i,
				use: ['style-loader', 'css-loader'],
				exclude: /node_modules/,
			},
			{
				test: /\.(woff|woff2|ttf|eot)$/,
				type: 'asset/resource',
				exclude: /node_modules/,
			},
		],
	},
	plugins: [
		new HtmlWebpackPlugin({
			title: "React App",
			template: './public/index.html',
		}),
	],
	resolve: {
		extensions: ['.tsx', '.ts', '.js'],
	},
	output: {
		filename: 'bundle.js',
		path: path.resolve(__dirname, 'dist'),
		publicPath: '/',
	},
};