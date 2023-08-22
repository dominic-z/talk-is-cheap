



const configJson = {
    "mode": "dev",
    "baseUri": "/api",
    "dev": {
        "ip": "http://localhost",
        "port": 8080
    }
}

interface BackendAddress {
    ip: string;
    port: number
}


class Config {
    private mode: string;
    private baseUri: string;
    private backendAddress: BackendAddress;

    constructor() {
        this.mode = configJson.mode;
        const addressJson = configJson.dev;

        this.baseUri = configJson.baseUri;
        this.backendAddress = {
            ip: addressJson.ip,
            port: addressJson.port
        }
    }

    getBaseUrl(): string {
        return this.backendAddress.ip + ":" + this.backendAddress.port+this.baseUri;
    }

}

export default new Config();