FROM python:3.11

WORKDIR /app

RUN apt-get update && \
    apt-get install -y gcc g++ cmake python3-dev make libopenblas-dev git && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .

RUN pip install --upgrade pip
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
