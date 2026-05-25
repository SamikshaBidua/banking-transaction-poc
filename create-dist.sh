#!/bin/bash
set -e

DIST_DIR="banking-transaction-poc"
ZIP_FILE="${DIST_DIR}.zip"

# Clean up any previous dist
rm -rf "$DIST_DIR" "$ZIP_FILE"

# Create structure
mkdir -p "$DIST_DIR/backend"
mkdir -p "$DIST_DIR/frontend/build"

# Copy pre-built backend artifact
cp backend/target/banking-transaction-poc-1.0.0.jar "$DIST_DIR/backend/"

# Copy pre-built frontend artifacts
cp -R frontend/build/* "$DIST_DIR/frontend/build/"

# Copy dist template files
cp dist-template/backend/Dockerfile "$DIST_DIR/backend/"
cp dist-template/frontend/Dockerfile "$DIST_DIR/frontend/"
cp dist-template/frontend/nginx.conf "$DIST_DIR/frontend/"
cp dist-template/docker-compose.yml "$DIST_DIR/"
cp dist-template/SOP.md "$DIST_DIR/"

# Create zip
zip -r "$ZIP_FILE" "$DIST_DIR"

echo ""
echo "Created: $ZIP_FILE"
ls -lh "$ZIP_FILE"
echo ""
echo "Contents:"
unzip -l "$ZIP_FILE" | tail -20
